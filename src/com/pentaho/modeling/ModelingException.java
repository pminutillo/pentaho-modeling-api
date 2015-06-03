/*
// $Id:  $
// (C) Copyright 2006-2006 LucidEra, Inc.
*/
package com.pentaho.modeling;

import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;

/**
 * InlineModelingException occurs if a model annotation is unsuccessfully applied to a catalog.
 * This can happen for many reasons such an annotation referencing a level or measure
 * that no longer exists in the catalog.  Other exceptions could occur if someone
 * tried to create a hierarchy using levels coming from different tables with no join 
 * path, etc....
 *
 * @author benny
 * @version $Id: $
 * @created Dec 28, 2014
 * @updated $DateTime: $
 */
public class ModelingException extends RuntimeException {

  private ModelAnnotation annotation;

  public ModelingException( String message, Throwable cause ) {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

  public ModelingException( String message ) {
    super( message );
    // TODO Auto-generated constructor stub
  }

  public ModelingException( Throwable cause ) {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  public ModelAnnotation getAnnotation() {
    return annotation;
  }

  public void setAnnotation( ModelAnnotation annotation ) {
    this.annotation = annotation;
  }

}
