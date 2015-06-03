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
package com.pentaho.modeling.service;

import com.pentaho.modeling.AnnotationResult;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;

/**
 * Add a layer of abstraction between the modeling service and Analyzer, in
 * case there is a need different implementation types
 *
 * Created by pminutillo on 12/12/14. Currently pseduocode to help sort out logic
 */
public interface IModelingServiceFacade {

  /**
   * Finds a Mondrian catalog and applies model changes to the catalog's XML.
   * If a catalog was generated by a DSW, then we need to apply the changes 
   * on a copy of the DSW Metadata and then re-generate the schema XML from that.  Otherwise,
   * if the mondrian schema XML was standalone, then we directly modify a copy of 
   * the XML.
   * 
   * @param catalogParam
   * @param annotations
   * @return
   */
  public AnnotationResult applyAnnotations( String catalogParam, ModelAnnotationGroup annotations );

  public boolean saveModel( String catalogParam, String newCatalogName, ModelAnnotationGroup annotations, boolean overwrite );

}
