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

/**
 * Created by pminutillo on 3/12/15.
 */

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.plugin.action.mondrian.MondrianCachePublisher;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;

/**
 * Extend the DataSourceWizardService, but add a method which accepts a domain parameter
 * and use IPlatformImporter to import DSW data source using import bundles
 */
public class ModelingDataSourceWizardImportService extends DataSourceWizardService {
  private final Log logger = LogFactory.getLog( DataSourceWizardService.class );


  private final String METADATA_PUBLISHER = MetadataPublisher.class.getName();
  private final String MONDRIAN_PUBLISHER = MondrianCachePublisher.class.getName();

  public String publishDsw( String domainId, Domain domain, boolean overwrite, boolean checkConnection )
    throws Exception {

    if ( !endsWith( domainId, ModelingConstants.METADATA_EXT ) ) {
      domainId = domainId.concat( ModelingConstants.METADATA_EXT );
    }
    if ( domain == null ) {
      throw new IllegalArgumentException( "domain is null" );
    }
    if ( !overwrite ) {
      final List<String> overwritten = getOverwrittenDomains( domainId );
      if ( !overwritten.isEmpty() ) {
        final String domainIds = StringUtils.join( overwritten, "," );
        throw new DswPublishValidationException( DswPublishValidationException.Type.OVERWRITE_CONFLICT, domainIds );
      }
    }

    XmiParser xmiParser = createXmiParser();

    domain.setId( domainId );
    if ( checkConnection ) {
      final String connectionId = getMondrianDatasourceWrapper( domain );
      if ( datasourceMgmtSvc.getDatasourceByName( connectionId ) == null ) {
        final String msg = "connection not found: '" + connectionId + "'";
        throw new DswPublishValidationException( DswPublishValidationException.Type.MISSING_CONNECTION, msg );
      }
    }
    // build bundles
    InputStream metadataIn = toInputStreamWrapper( domain, xmiParser );
    IPlatformImportBundle metadataBundle = createMetadataDswBundle( domain, metadataIn, overwrite, null );
    IPlatformImportBundle mondrianBundle = createMondrianDswBundle( domain, null );
    // do import
    IPlatformImporter importer = getIPlatformImporter();
    importer.importFile( metadataBundle );
    logger.debug( "imported metadata xmi" );
    importer.importFile( mondrianBundle );
    logger.debug( "imported mondrian schema" );
    // trigger refreshes
    IPentahoSession session = getSession();
    PentahoSystem.publish( session, METADATA_PUBLISHER );
    PentahoSystem.publish( session, MONDRIAN_PUBLISHER );
    logger.info( "publishDsw: Published DSW with domainId='" + domainId + "'." );
    return domainId;
  }
}
