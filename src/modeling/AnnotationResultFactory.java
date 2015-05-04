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

package modeling;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;

/**
 * Factory to create AnnotationResult instances based on provided parameter
 * Domain = DSw, String (schema) = Mondrian
 *
 * Created by pminutillo on 3/2/15.
 */
public class AnnotationResultFactory {

  static ModelingDataSourceWizardImportService modelingDataSourceWizardImportService;
  static IMondrianCatalogService mondrianCatalogService;

  public static AnnotationResult createResult( Domain domain ){
    DswAnnotationResult result = new DswAnnotationResult();
    if( modelingDataSourceWizardImportService != null ){
      result.setModelingDataSourceWizardImportService( modelingDataSourceWizardImportService );
    }
    result.setDomain( domain );
    return result;
  }

  public static AnnotationResult createResult( Domain domain, String schema ){
    DswAnnotationResult result = new DswAnnotationResult();
    if( modelingDataSourceWizardImportService != null ){
      result.setModelingDataSourceWizardImportService( modelingDataSourceWizardImportService );
    }
    result.setDomain( domain );
    result.setSchema( schema );
    return result;
  }

  public static AnnotationResult createResult( String schema ){
    MondrianAnnotationResult result = new MondrianAnnotationResult();
    if( mondrianCatalogService != null ){
      result.setMondrianCatalogService( mondrianCatalogService );
    }
    result.setSchema( schema );
    return result;
  }

  public static ModelingDataSourceWizardImportService getModelingDataSourceWizardImportService() {
    return modelingDataSourceWizardImportService;
  }

  public static void setModelingDataSourceWizardImportService(
    ModelingDataSourceWizardImportService newModelingDataSourceWizardImportService ) {
    modelingDataSourceWizardImportService = newModelingDataSourceWizardImportService;
  }

  public static IMondrianCatalogService getMondrianCatalogService() {
    return mondrianCatalogService;
  }

  public static void setMondrianCatalogService( IMondrianCatalogService mondrianCatalogService ) {
    AnnotationResultFactory.mondrianCatalogService = mondrianCatalogService;
  }
}
