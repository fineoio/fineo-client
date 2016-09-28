package io.fineo.client.tools.option;

import io.fineo.client.tools.EventTypes;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class TestSchemaOption {

  @Test
  public void testLoadFields() throws Exception {
    SchemaOption opt = new SchemaOption();
    opt.type = MetricClass.class.getName();

    List<SchemaOption.FieldInstance> fields = opt.getFields();
    assertEquals("Missing some fields! Got fields: " + fields, 7, fields.size());
    // sort by the number after the first character
    fields.sort((f1, f2) -> {
      String i = f1.name.substring(1);
      String i2 = f2.name.substring(1);
      Integer ii1 = Integer.valueOf(i);
      Integer ii2 = Integer.valueOf(i2);
      return ii1.compareTo(ii2);
    });
    List<String> types = newArrayList("string", "integer", "long", "double", "boolean", "binary",
      "float");
    fields.forEach(field -> {
      assertEquals("Mismatch for field: " + field.name,
        types.remove(0).toUpperCase(), field.type.toUpperCase());
    });

    assertEquals(opt.type, opt.name);
  }

  public static class MetricClass extends EventTypes.EventBase {
    private String f1;
    private int f2;
    private long f3;
    private double f4;
    private boolean f5;
    private byte[] f6;
    private float f7;

    public String getF1() {
      return f1;
    }

    public void setF1(String f1) {
      this.f1 = f1;
    }

    public int getF2() {
      return f2;
    }

    public void setF2(int f2) {
      this.f2 = f2;
    }

    public long getF3() {
      return f3;
    }

    public void setF3(long f3) {
      this.f3 = f3;
    }

    public double getF4() {
      return f4;
    }

    public void setF4(double f4) {
      this.f4 = f4;
    }

    public boolean getF5() {
      return f5;
    }

    public void setF5(boolean f5) {
      this.f5 = f5;
    }

    public byte[] getF6() {
      return f6;
    }

    public void setF6(byte[] f6) {
      this.f6 = f6;
    }

    public float getF7() {
      return f7;
    }

    public void setF7(float f7) {
      this.f7 = f7;
    }
  }
}
