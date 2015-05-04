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
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Utility methods used during inline modeling
 *
 * Created by pminutillo on 3/2/15.
 */
public class ModelingUtil {
  /**
   *
   * @param inputStream
   * @return
   * @throws Exception
   */
  public static Document parseXmlToDoc( InputStream inputStream ) throws Exception {
    Document result;

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    result = dBuilder.parse( inputStream );
    inputStream.close();
    result.getDocumentElement().normalize();

    return result;
  }


  /**
   * Serialize schema file back into bytes
   * @param doc
   * @return
   */
  public static String xmlDocToString( Document doc ){
    try {

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );

      StringWriter writer = new StringWriter();
      DOMSource source = new DOMSource( doc );
      StreamResult result = new StreamResult( writer );
      // Output to console for testing
      // StreamResult result = new StreamResult(System.out);
      transformer.transform( source, result );
      return writer.toString();
    }
    catch( Exception e ){
      throw new InlineModelingException( e );
    }
  }
}
