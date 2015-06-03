/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.modeling;

import java.io.ByteArrayInputStream;

import com.pentaho.modeling.util.ModelingUtil;
import com.pentaho.modeling.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.MondrianCachePublisher;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulate the logic for saving a Mondrian schema modified during inline modeling
 * <p/>
 * Created by pminutillo on 3/2/15.
 */
public class MondrianAnnotationResult extends AnnotationResult {

  private final Log logger = LogFactory.getLog( MondrianAnnotationResult.class );

  private static final String MONDRIAN_PUBLISHER = MondrianCachePublisher.class.getName();
  private static final String ENCODING = "UTF-8";
  private static final String MONDRIAN_CONNECTION_PARAM = "parameters";
  private static final String MONDRIAN_SCHEMA_NAME = "schema.xml";
  private static final String MONDRIAN_MIME = "application/vnd.pentaho.mondrian+xml";
  private static final String IMPORT_DOMAIN_ID = "domain-id";

  private IMondrianCatalogService mondrianCatalogService;

  public MondrianAnnotationResult() {
    super( type.MONDRIAN );
  }

  /**
   * Save by building an import bundle and using IPlatformImporter
   *
   * @param catalogName
   * @param newCatalogName
   * @param overwrite
   * @return
   */
  @Override public boolean save( String catalogName, String newCatalogName, boolean overwrite ) {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();

    String mondrianSchemaXml = this.getSchema();

    try {
      Document mondrianSchemaDoc = ModelingUtil.parseXmlToDoc(
        new ByteArrayInputStream( mondrianSchemaXml.getBytes() ) );
      updateSchemaNodeName( mondrianSchemaDoc, newCatalogName );
      mondrianSchemaXml = ModelingUtil.xmlDocToString( mondrianSchemaDoc );

      if ( mondrianCatalogService == null ) {
        mondrianCatalogService =
          PentahoSystem.get( IMondrianCatalogService.class,
            "IMondrianCatalogService", pentahoSession ); //$NON-NLS-1$
      }

      MondrianCatalog catalog = mondrianCatalogService.getCatalog( catalogName, pentahoSession );

      // create bundle
      IPlatformImportBundle mondrianBundle = new RepositoryFileImportBundle.Builder()
          .input( IOUtils.toInputStream( mondrianSchemaXml, ENCODING ) )
          .name( MONDRIAN_SCHEMA_NAME )
          .charSet( ENCODING )
          .overwriteFile( true )
          .mime( MONDRIAN_MIME )
          .withParam( IMPORT_DOMAIN_ID, newCatalogName )
          .withParam( MONDRIAN_CONNECTION_PARAM, catalog.getDataSourceInfo() )
          .build();

      // do import
      IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

      importer.importFile( mondrianBundle );
      logger.debug( "imported mondrian schema" );
      // trigger refreshes
      IPentahoSession session = PentahoSessionHolder.getSession();
      PentahoSystem.publish( session, MONDRIAN_PUBLISHER );
      logger.info( "Published Mondrian schema with catalogId='" + newCatalogName + "'." );
    } catch ( Exception e ) {
      throw new ModelingException( e );
    }

    return true;
  }

  /**
   * Update catalog definition URL to new name
   *
   * @param oldCatalogDefinition
   * @param catalogName
   * @return
   */
  private String updateCatalogDefinitionString( String oldCatalogDefinition, String catalogName ) {
    String newCatalogDefinition = oldCatalogDefinition;

    String stringToReplace = oldCatalogDefinition.substring( oldCatalogDefinition.indexOf( '/' ) + 1 );
    if ( StringUtil.isEmpty( stringToReplace ) ) {
      return newCatalogDefinition;
    }

    return oldCatalogDefinition.replace( stringToReplace, catalogName );
  }

  /**
   * Update the value of the name attribute of the schema node e.g. <schema name="xxxxxxxxx"></schema>
   *
   * @param schema
   * @return
   */
  private Document updateSchemaNodeName( Document schema, String newSchemaName ) {
    NodeList schemaElements = schema.getElementsByTagName( ModelingConstants.SCHEMA_TAG_NAME );

    if ( ( schemaElements == null )
      || ( schemaElements.getLength() <= 0 ) ) {
      return null;
    }

    Node schemaNode = schemaElements.item( 0 );
    NamedNodeMap namedNodeMap = schemaNode.getAttributes();
    Node nameNode = namedNodeMap.getNamedItem( ModelingConstants.SCHEMA_NODE_NAME_ATTRIBUTE );
    if ( nameNode != null ) {
      nameNode.setNodeValue( newSchemaName );
    }

    return schema;
  }

  public IMondrianCatalogService getMondrianCatalogService() {
    return mondrianCatalogService;
  }

  public void setMondrianCatalogService( IMondrianCatalogService mondrianCatalogService ) {
    this.mondrianCatalogService = mondrianCatalogService;
  }
}
