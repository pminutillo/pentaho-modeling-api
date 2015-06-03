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

import org.pentaho.metadata.model.Domain;

/**
 * A class to hold fields relevant to both Mondrian and DSW data sources, as
 * well as encapsulate the logic for saving each data source type via abstract
 * save() method
 *
 * Created by pminutillo on 3/1/15.
 */
public abstract class AnnotationResult {
  private Domain domain;
  private String schema;

  private AnnotationResult.type resultType;

  public AnnotationResult(){

  }

  public AnnotationResult( type resultType ){
    this.resultType = resultType;
  }

  public Domain getDomain() {
    return domain;
  }

  public void setDomain( Domain domain ) {
    this.domain = domain;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema( String schema ) {
    this.schema = schema;
  }

  public type getResultType() {
    return resultType;
  }

  public void setResultType( type resultType ) {
    this.resultType = resultType;
  }

  public enum type{
    MONDRIAN,
    DSW
  }

  /**
   * Save method for each data source type to be implemented
   *
   * @return
   */
  public abstract boolean save( String catalogName, String newCatalogName, boolean overwrite );
}
