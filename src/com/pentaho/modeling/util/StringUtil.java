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

package com.pentaho.modeling.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Utility Class This is used to encode passwords programmatically
 * 
 */
public class StringUtil implements java.io.Serializable {

  private static final long serialVersionUID = -890747000399519956L; /* EESOURCE: UPDATE SERIALVERUID */
  // ~ Static fields/initializers =============================================

  private static final Log log = LogFactory.getLog( StringUtil.class );

  private static final Pattern numberPattern = Pattern.compile( "^[0-9]{0,}(\\.[0-9]{0,})?$" );

  /* START LICENSE CHECK */
  /**
   * Build replaces this token. Name of variable is contrived.
   */
  public static final String BUNDLE_REF_NAME = "@VERSION_FOR_LICENSE@"; //$NON-NLS-1$

  /* END LICENSE CHECK */

  // ~ Methods ================================================================

  public static boolean isEmpty( String s ) {
    if ( s == null || s.equals( "" ) ) {
      return true;
    }
    return false;
  }

  public static boolean isEmpty( Integer s ) {
    if ( s == null || s.intValue() == 0 ) {
      return true;
    }
    return false;
  }

  public static String quoteCubeName( String cubeName ) {
    if ( cubeName == null ) {
      return null;
    }

    String quotedName = cubeName;

    if ( quotedName.charAt( 0 ) != '[' ) {
      quotedName = '[' + quotedName;
    }

    // double escape any closing brackets if there are any
    if ( quotedName.indexOf( ']' ) > -1 ) {
      quotedName = quotedName.replace( "]", "]]" );
    }

    if ( quotedName.charAt( quotedName.length() - 1 ) != ']' ) {
      quotedName = quotedName + ']';
    }
    return quotedName;
  }

  public static String unquoteCubeName( String cubeName ) {
    if ( cubeName == null ) {
      return null;
    }

    int beginIndex = 0;
    int endIndex = cubeName.length() - 1;

    if ( cubeName.charAt( beginIndex ) == '[' ) {
      beginIndex += 1;
    }

    if ( cubeName.charAt( endIndex ) == ']' ) {
      endIndex -= 1;
    }

    // unescape any double closing brackets
    String[] tokens = cubeName.split( "]]" );
    if ( tokens.length > 1 ) {
      cubeName = cubeName.replace( "]]", "]" );
      endIndex -= ( tokens.length - 1 );
    }

    return cubeName.substring( beginIndex, endIndex + 1 );
  }

  /**
   * covert an array of bytes to a hex string
   * 
   * @param bytes
   *          an array of bytes to convert to hex
   * @return String the hex string representation of the bytes
   * 
   */
  public static String toHexString( byte[] bytes ) {
    StringBuilder builder = new StringBuilder();
    for ( byte b : bytes ) {
      builder.append( toHexString( b ) );
    }
    return builder.toString();
  }

  /**
   * covert a byte to a hex string
   * 
   * @param b
   *          a byte to convert to hex
   * @return String the hex string representation of the byte
   * 
   */
  public static String toHexString( byte b ) {
    int i = ( ( (int) b ) << 24 ) >>> 24;

    if ( i < (byte) 16 ) {
      return "0" + Integer.toString( i, 16 );
    } else {
      return Integer.toString( i, 16 );
    }
  }

  /**
   * Determine whether this string is numberic
   * 
   * @param numberStr
   *          a string which need to verify it's numberic or not.
   * @return true it's numberic, false it's not.
   * 
   */
  public static boolean isNumber( String numberStr ) {
    Matcher mather = numberPattern.matcher( numberStr );
    if ( mather.matches() ) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Remove trailing whitespaces from a string
   * 
   * @param source
   *          the input String
   * @return String with the trailing whitespaces removed.
   * 
   */
  public static String rtrim( String source ) {
    return source.replaceAll( "\\s+$", "" );
  }

  /**
   * convert the string to list,if str or delim is null,it will throw NullPointException
   * 
   * @param str
   *          a string to be parsed.
   * @param delim
   *          the delimiters.
   * @return the list of String
   */
  public static List<String> stringToList( String str, String delim ) {
    List<String> result = new ArrayList<String>();
    StringTokenizer ssTr = new StringTokenizer( str, delim );
    while ( ssTr.hasMoreTokens() ) {
      String temStr = ssTr.nextToken();
      result.add( temStr );

    }
    return result;
  }

  /**
   * replace all special characters with underscore
   * 
   * @param s
   *          the input String
   * @return String with the special characters substituted.
   */
  public static String escapeString( String s ) {
    return s.replaceAll( "[^\\w\\s-_]", "_" );
  }

  /**
   * Quote a string using double-quote
   * 
   * @param s
   *          The string to be quoted
   * @return String
   */
  public static String quoteString( String s ) {
    StringBuffer sb = new StringBuffer( "\"" );
    sb.append( s ).append( "\"" );
    return sb.toString();
  }
}
