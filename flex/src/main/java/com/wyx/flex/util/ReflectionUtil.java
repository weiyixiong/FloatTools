package com.wyx.flex.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtil {

  public static Class getClass(String classname) {
    Class result = null;
    try {
      result = Class.forName(classname);
    } catch (ClassNotFoundException e4) {
      L.exception(e4);
    }
    return result;
  }

  /**
   * 与findField同理
   */
  public static Method getMethod(String methodName, Class clazz, Class... parameterTypes) {
    Class searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Method[] methods = searchType.getDeclaredMethods();
      for (Method method : methods) {
        if ((methodName == null || methodName.equals(method.getName())) &&
            (parameterTypes == null || Arrays.equals(parameterTypes, method.getParameterTypes()))) {
          method.setAccessible(true);
          return method;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  public static Method getMethod(String methodName, String className, Class... parameterTypes) {
    return getMethod(methodName, getClass(className), parameterTypes);
  }

  /**
   * 反射必须保证当前类存在此Field,子类里查找不到父类Field,所以要从子类向父类遍历查找
   */
  public static Field getField(Class clazz, String name, Class type) {
    Class searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Field[] fields = searchType.getDeclaredFields();
      for (Field field : fields) {
        if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
          field.setAccessible(true);
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  public static Object getObject(Class classz) {
    Object result = null;
    try {
      result = classz.newInstance();
    } catch (InstantiationException e3) {
      L.exception(e3);
    } catch (IllegalAccessException e3) {
      L.exception(e3);
    }
    return result;
  }

  public static Object getObject(String classname) {
    Object result = null;
    try {
      result = getClass(classname).newInstance();
    } catch (InstantiationException e3) {
      L.exception(e3);
    } catch (IllegalAccessException e3) {
      L.exception(e3);
    }
    return result;
  }
}