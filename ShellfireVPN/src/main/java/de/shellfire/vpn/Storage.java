/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn;

import java.util.HashMap;

/**
 *
 * @author bettmenn
 */
@SuppressWarnings("rawtypes")
public class Storage {

  private static HashMap<Class, Object> objects = new HashMap<Class, Object>();

  public static void register(Object o) {
    Class c = o.getClass();
    Storage.objects.put(c, o);
  }

  public static Object get(Class c) {
    Object o = Storage.objects.get(c);

    return o;
  }

}
