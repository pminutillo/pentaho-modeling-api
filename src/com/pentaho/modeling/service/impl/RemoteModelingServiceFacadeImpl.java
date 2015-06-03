/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.modeling.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.pentaho.modeling.ModelingException;
import com.pentaho.modeling.service.AbstractMetastoreServiceFacade;

import com.pentaho.modeling.ModelingWorkspaceHelper;
import com.pentaho.modeling.AnnotationResult;
import com.pentaho.modeling.AnnotationResultFactory;
import com.pentaho.modeling.ModelingConstants;
import com.pentaho.modeling.service.IModelingServiceFacade;
import com.pentaho.modeling.util.ModelingUtil;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;

import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

import org.pentaho.platform.repository.solution.filebased.SolutionRepositoryVfsFileObject;

import org.w3c.dom.Document;

/**
 * Implement the modeling service
 * <p/>
 * Created by pminutillo on 12/12/14.
 */
public class RemoteModelingServiceFacadeImpl implements IModelingServiceFacade {


  private final String locale = Locale.getDefault().toString();

  /**
   * Provided at catalog name, find an associated Domain
   *
   * @param catalogParam
   * @return
   */
  private Domain getDomain( String catalogParam ) {
    // First, we need to see if this catalog is based on a DSW datasource
    // or just a standalone Mondrian schema file.
    IMetadataDomainRepository metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, null );
    Set<String> domainIds = metadataDomainRepository.getDomainIds();
    Domain domain = null;

    exit:
    for ( String domainId : domainIds ) {
      Domain currentDomain = metadataDomainRepository.getDomain( domainId );
      List<LogicalModel> models = currentDomain.getLogicalModels();
      if ( models.size() == 0 ) {
        continue; // ESR-1130 Skip incomplete models
      }

      for ( LogicalModel lModel : currentDomain.getLogicalModels() ) {
        String catalog = (String) lModel.getProperty( ModelingConstants.MONDRIAN_CATALOG_REF_PROPERTY );
        if ( catalog != null && catalog.equals( catalogParam ) ) {
          // Serialize and then de-serialize to make a deep copy of domain
          XmiParser parser = new XmiParser();
          String localXmi = parser.generateXmi( currentDomain );
          InputStream is = new ByteArrayInputStream( localXmi.getBytes() );
          try {
            domain = parser.parseXmi( is );
            domain.setId( domainId );
          } catch ( Exception e ) {
            throw new ModelingException( e );
          }
          break exit;
        }
      }
    }

    return domain;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.pentaho.analyzer.service.IModelingServiceFacade#applyAnnotations(java.lang.String,
   * org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup)
   */
  public AnnotationResult applyAnnotations( String catalogParam, ModelAnnotationGroup annotations ) {
    Domain domain = getDomain( catalogParam );

    if ( domain != null ) {
      // We found a DSW so apply the changes on the metadata model
      return applyAnnotationsOnMetadataModel( domain, annotations );
    } else {
      // We didn't find a DSW to apply changes directly on the Mondrian XML file
      return applyAnnotationsOnMondrianModel( catalogParam, annotations );
    }
  }

  /**
   * Reads in a Mondrian schema file and then applied model changes directly on the XML file.
   *
   * @param catalogParam
   * @param annotations
   * @return
   */
  private AnnotationResult applyAnnotationsOnMondrianModel( String catalogParam, ModelAnnotationGroup annotations ) {
    Document doc;

    try {
      // This is a stand alone Mondrian model so directly load up the XML
      IMondrianCatalogService mondrianCatalogService =
          PentahoSystem.get( IMondrianCatalogService.class,
          "IMondrianCatalogService", PentahoSessionHolder.getSession() ); //$NON-NLS-1$
      MondrianCatalog catalog = mondrianCatalogService.getCatalog( catalogParam, PentahoSessionHolder.getSession() );
      if ( catalog == null ) {
        throw new ModelingException( "Unable to find catalog: " + catalogParam );
      }
      FileSystemManager fsManager = VFS.getManager();
      SolutionRepositoryVfsFileObject mondrianDS =
          (SolutionRepositoryVfsFileObject) fsManager.resolveFile( catalog.getDefinition() );

      // Read the XML file from the repository
      IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class, PentahoSessionHolder.getSession() );
      RepositoryFile schemaFile = repository.getFile( mondrianDS.getFileRef() );
      SimpleRepositoryFileData fileData =
          repository.getDataForRead( schemaFile.getId(), SimpleRepositoryFileData.class );
      InputStream inputStream = fileData.getInputStream();

      // Parse XML file into an XML document
      doc = ModelingUtil.parseXmlToDoc( inputStream );

      for ( ModelAnnotation modelAnnotation : annotations ) {
        if ( !modelAnnotation.apply( doc ) ) {
          ModelingException e = new ModelingException( "Unable to apply model change" );
          e.setAnnotation( modelAnnotation );
          throw e;
        }
      }

    } catch ( Exception e ) {
      throw new ModelingException( e );
    }

    return AnnotationResultFactory.createResult( ModelingUtil.xmlDocToString( doc ) );
  }


  /**
   * Applies model changes on a Metadata OLAP logical model.
   *
   * @param domain
   * @param annotations
   * @return
   */
  private AnnotationResult applyAnnotationsOnMetadataModel( Domain domain, ModelAnnotationGroup annotations ) {
    ModelerWorkspace workspace;
    MondrianModelExporter exporter;
    String schemaXml;

    try {
      AbstractMetastoreServiceFacade metastoreServiceFacade = PentahoSystem.get( AbstractMetastoreServiceFacade.class );
      // Apply annotations
      workspace =
        new ModelerWorkspace( new ModelingWorkspaceHelper(), ModelingWorkspaceHelper.initGeoContext() );
      workspace.setDomain( domain );

      for ( ModelAnnotation modelAnnotation : annotations ) {
        if ( !modelAnnotation.apply( workspace, metastoreServiceFacade.getMetaStore() ) ) {
          ModelingException e = new ModelingException( "Unable to apply model change" );
          e.setAnnotation( modelAnnotation );
          throw e;
        }
      }

      // Export to Mondrian XML
      LogicalModel olapModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
      // reference schema in xmi
      // olapModel.setProperty( MONDRIAN_CATALOG_REF, analysisDomainId );
      // generate schema
      exporter = new MondrianModelExporter( olapModel, locale );
      schemaXml = exporter.createMondrianModelXML();

    } catch ( Exception e ) {
      throw new ModelingException( e );
    }

    return AnnotationResultFactory.createResult( workspace.getDomain(), schemaXml );
  }

  /**
   * Save the model by updating the associated Mondrian catalog or Metadata datasource
   *
   * @param catalogName
   * @return
   */
  public boolean saveModel( String catalogName, String newCatalogName, ModelAnnotationGroup annotations,
                            boolean overwrite ) {
    AnnotationResult annotationResult = applyAnnotations( catalogName, annotations );

    if ( overwrite ) {
      newCatalogName = catalogName;
    }

    annotationResult.save( catalogName, newCatalogName, overwrite );

    return true;
  }


}
