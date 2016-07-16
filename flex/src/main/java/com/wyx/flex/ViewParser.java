package com.wyx.flex;

import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by winney on 16/4/28.
 */
public class ViewParser {

  private static final byte SIG_BOOLEAN = 'Z';//90
  private static final byte SIG_BYTE = 'B';//66
  private static final byte SIG_SHORT = 'S';//83
  private static final byte SIG_INT = 'I';//73
  private static final byte SIG_LONG = 'J';//74
  private static final byte SIG_FLOAT = 'F';//70
  private static final byte SIG_DOUBLE = 'D';//68

  // Prefixes for some commonly used objects
  private static final byte SIG_STRING = 'R';//82

  private static final byte SIG_MAP = 'M'; //77 a map with an short key
  private static final short SIG_END_MAP = 0;
  private static Map<Short, String> mKeyValue = new HashMap<>(200);
  private static DataInputStream mStream;
  private static Charset mCharset = Charset.forName("utf-8");

  private static Stack<Map<Short, String>> stack = new Stack<>();
  private static List<Map<Short, String>> result = new ArrayList<>();
  private static Map<String, Map<String, String>> mergeResult = new HashMap<>();

  private static ViewDataNode root;
  private static ViewDataNode currentNode;

  public static void parser(byte[] bytes) throws IOException {
    parseByte(bytes);
    mergeResult();
  }

  private static void mergeResult() {
    mKeyValue = result.get(result.size() - 1);
    result.remove(result.size() - 1);
    for (Map<Short, String> shortStringMap : result) {
      Map<String, String> tmp = new HashMap<>();
      for (Map.Entry<Short, String> shortStringEntry : shortStringMap.entrySet()) {
        tmp.put(mKeyValue.get(shortStringEntry.getKey()), shortStringEntry.getValue());
      }
      mergeResult.put(shortStringMap.get((short) 2), tmp);
    }
    mergeViewData(mKeyValue, root);
  }

  private static void mergeViewData(Map<Short, String> mKeyValue, ViewDataNode root) {
    if (root.getOriginData() != null) {
      Map<String, String> tmp = new HashMap<>();
      for (Map.Entry<Short, String> shortStringEntry : root.getOriginData().entrySet()) {
        tmp.put(mKeyValue.get(shortStringEntry.getKey()), shortStringEntry.getValue());
      }
      root.setData(tmp);
    }

    if (root.child != null) {
      for (ViewDataNode viewDataNode : root.child) {
        mergeViewData(mKeyValue, viewDataNode);
      }
    }
  }

  private static boolean isLayoutParams = false;

  private static String debugName;

  private static void parseByte(byte[] bytes) throws IOException {
    mStream = new DataInputStream(new ByteArrayInputStream(bytes));
    boolean output = true;
    root = new ViewDataNode(null);
    currentNode = root;

    while (mStream.available() != 0) {
      short name = 0;

      int readRes = mStream.read();
      if (readRes == SIG_SHORT) {
        name = readShort();
        if (name == SIG_END_MAP) {
          L.e("end-------------------------->");
          result.add(mKeyValue);
          currentNode.setOriginData(mKeyValue);
          currentNode = currentNode.parent;
          if (!stack.isEmpty()) {
            mKeyValue = stack.pop();
          } else {
            mKeyValue = null;
          }
          continue;
        }
      } else if (readRes == SIG_MAP) {
        L.e("start-------------------------->");
        stack.push(mKeyValue);

        ViewDataNode viewDataNode = new ViewDataNode(currentNode);
        currentNode.addChild(viewDataNode);
        currentNode = viewDataNode;

        mKeyValue = new HashMap<>();
        continue;
      }

      int type = 0;
      try {
        type = mStream.read();
      } catch (IOException e) {
        e.printStackTrace();
      }
      switch (type) {
        case SIG_SHORT:
          short readShort = readShort();
          mKeyValue.put(name, readShort + "");

          if (readShort == SIG_END_MAP) {
            L.e("end-------------------------->");
            result.add(mKeyValue);
            mKeyValue = stack.pop();
            output = false;
          }
          break;
        case SIG_STRING:
          String viewClassName = readString();
          if (viewClassName.contains("android")) {
            String tmp = " ";
            for (int i = 0; i < stack.size(); i++) {
              tmp += "   ";
            }
            L.e(tmp + stack.size() + viewClassName);
          }
          //if (viewClassName.contains("$LayoutParams")) {
          //  //L.e(viewClassName);
          //  isLayoutParams = true;
          //  mKeyValue = stack.pop();
          //}
          mKeyValue.put(name, viewClassName + "");
          break;
        case SIG_INT:
          mKeyValue.put(name, readInt() + "");
          break;
        case SIG_DOUBLE:
          mKeyValue.put(name, readDouble() + "");
          break;
        case SIG_LONG:
          mKeyValue.put(name, readLong() + "");
          break;
        case SIG_FLOAT:
          mKeyValue.put(name, readFloat() + "");
          break;
        case SIG_BOOLEAN:
          mKeyValue.put(name, readBoolean() + "");
          break;
        case SIG_BYTE:
          mKeyValue.put(name, readByte() + "");
          break;
        case SIG_MAP:
          L.e("start-------------------------->");
          stack.push(mKeyValue);
          mKeyValue = new HashMap<>();
          ViewDataNode viewDataNode = new ViewDataNode(currentNode);
          currentNode.addChild(viewDataNode);
          currentNode = viewDataNode;
          output = false;
          break;
        default:
          mKeyValue.put(name, type + "");
      }
      if (output) {
        //L.e(name + "", " " + mKeyValue.get(name));
      } else {
        output = true;
      }
    }
  }

  public static List<Pair<String, String>> getViewInfo(int hashcode) {
    return getViewInfo(hashcode + "");
  }

  public static List<Pair<String, String>> getViewInfo(String hashcode) {
    List<Pair<String, String>> res = new ArrayList<>();
    Map<String, String> stringStringMap = mergeResult.get(hashcode);
    if (stringStringMap == null) {
      return null;
    }
    for (Map.Entry<String, String> stringStringEntry : stringStringMap.entrySet()) {
      res.add(new Pair<>(stringStringEntry.getKey(), stringStringEntry.getValue()));
    }
    return res;
  }

  private static byte readByte() {
    try {
      return mStream.readByte();
    } catch (IOException e) {
      return 0;
      // does not happen since the stream simply wraps a ByteArrayOutputStream
    }
  }

  private static boolean readBoolean() {
    try {
      return mStream.readByte() == 1;
    } catch (IOException e) {
      return false;
    }
  }

  private static float readFloat() {
    try {
      return mStream.readFloat();
    } catch (IOException e) {
      return 0;
    }
  }

  private static long readLong() {
    try {
      return mStream.readLong();
    } catch (IOException e) {
      return 0;
    }
  }

  private static double readDouble() {
    try {
      return mStream.readDouble();
    } catch (IOException e) {
      return 0;
    }
  }

  private static int readInt() {
    try {
      return mStream.readInt();
    } catch (IOException e) {
      return 0;
    }
  }

  private static short readShort() {
    try {
      return mStream.readShort();
    } catch (IOException e) {
      return 0;
    }
  }

  private static String readString() {
    try {
      short len = mStream.readShort();
      byte[] bytes = new byte[len];
      mStream.read(bytes, 0, len);
      return new String(bytes, mCharset);
    } catch (IOException e) {
      return "";
    }
  }
}
