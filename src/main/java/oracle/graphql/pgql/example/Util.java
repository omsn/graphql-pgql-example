/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package oracle.graphql.pgql.example;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class Util {

  public static String getResource(String path) throws IOException {
    URL url = Resources.getResource(path);
    return Resources.toString(url, Charsets.UTF_8);
  }

}
