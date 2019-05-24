package org.ananas.runner.misc;

import com.thoughtworks.xstream.XStream;
import java.io.*;
import org.apache.commons.io.IOUtils;

public class SerializationUtils {

  public static Object deserialize(String path, boolean useStream) {
    try {
      String serializedModelPath = path;
      File file = new File(serializedModelPath);
      FileInputStream fileInputStream = new FileInputStream(file);
      Object o = readObject(fileInputStream, useStream);
      fileInputStream.close();
      return o;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(
          "Oops could not find any model. Please train your model and test it. Details: "
              + e.getMessage());
    } catch (IOException e2) {
      throw new RuntimeException(
          "Oops an error occurred while storing trained model. Details : " + e2.getMessage());
    }
  }

  public static void serialize(Serializable model, String path, boolean useStream) {
    try {
      String serializedModelPath = path;
      File file = new File(serializedModelPath);
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      writeObject(model, fileOutputStream, useStream);
      fileOutputStream.close();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(
          "Oops could not store trained model in disk. Please try again or contact our support service. Details: "
              + e.getMessage());
    } catch (IOException e2) {
      throw new RuntimeException(
          "Oops an error occurred while storing trained model. Details : " + e2.getMessage());
    }
  }

  private static void writeObject(
      Serializable model, FileOutputStream fileOutputStream, boolean useStream) {
    if (useStream) {
      XStream xstream = new XStream();
      String xml = xstream.toXML(model);
      try {
        IOUtils.write(xml, fileOutputStream);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      org.apache.commons.lang3.SerializationUtils.serialize(model, fileOutputStream);
    }
  }

  private static Object readObject(FileInputStream fileInputStream, boolean useStream) {
    if (useStream) {
      XStream xstream = new XStream();
      return xstream.fromXML(fileInputStream);
    } else {
      return org.apache.commons.lang3.SerializationUtils.deserialize(fileInputStream);
    }
  }
}
