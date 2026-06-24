package local.sop.sopinfo.person.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.ArchConfiguration;

import org.junit.jupiter.api.Test;

public class ArchPropsSanityTest {
    @Test void props_loaded() {
    var v = ArchConfiguration.get().getProperty("archRule.failOnEmptyShould");
    assertTrue(v.equals("false"));
  }

}
