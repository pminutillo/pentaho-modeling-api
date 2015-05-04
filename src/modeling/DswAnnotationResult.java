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

import com.pentaho.analyzer.service.InlineModelingException;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;

import java.util.Locale;

/**
 * Encapsulate the logic for saving a domain modified via inline modeling to a DSW data source
 *
 * Created by pminutillo on 3/2/15.
 */
public class DswAnnotationResult extends AnnotationResult {
  ModelingDataSourceWizardImportService modelingDataSourceWizardImportService;

  public DswAnnotationResult(){
    super( AnnotationResult.type.DSW );
  }

  @Override public boolean save( String catalogName, String newCatalogName, boolean overwrite ) {
    final String locale = Locale.getDefault().toString();

    // Update dsw datasource with new catalog
    for ( LogicalModel lModel : this.getDomain().getLogicalModels() ) {
      String catalog = (String) lModel.getProperty( ModelingConstants.MONDRIAN_CATALOG_REF_PROPERTY );
      if ( catalog != null && catalog.equals( catalogName ) ) {

        // set mondrianCatalogRef property
        lModel.setProperty( ModelingConstants.MONDRIAN_CATALOG_REF_PROPERTY, newCatalogName );

        // AND change the localized name property
        LocalizedString localizedName = (LocalizedString) lModel.getProperty( ModelingConstants.OLAP_LOCALIZED_NAME_PROPERTY );
        if( localizedName != null ){
          localizedName.setString( locale, newCatalogName.concat( ModelingConstants.OLAP_NAME_EXTENSION ) );
          lModel.setProperty( ModelingConstants.OLAP_LOCALIZED_NAME_PROPERTY, localizedName );
        }

        break;
      }
    }

    if( modelingDataSourceWizardImportService == null ) {
      modelingDataSourceWizardImportService = new ModelingDataSourceWizardImportService();
    }

    try {
      if( !overwrite ) {
        this.getDomain().setId( newCatalogName );
      }

      modelingDataSourceWizardImportService.publishDsw( newCatalogName, this.getDomain(), overwrite, Boolean.FALSE );

    } catch ( Exception e ) {
      e.printStackTrace();
      throw new InlineModelingException( "Error publishing DSW datasource with a modified model"  );
    }

    return true;
  }

  public ModelingDataSourceWizardImportService getModelingDataSourceWizardImportService() {
    return modelingDataSourceWizardImportService;
  }

  public void setModelingDataSourceWizardImportService(
    ModelingDataSourceWizardImportService modelingDataSourceWizardImportService ) {
    this.modelingDataSourceWizardImportService = modelingDataSourceWizardImportService;
  }

}
