package cs550.pa3.test;

import cs550.pa3.helpers.Host;
import org.junit.Test;

/**
 * Created by Ajay on 3/19/17.
 */
public class HostTest {

  @Test
  public void getHashCode() throws Exception {
    Host h1 = new Host("123.4.4.2",8080);
    Host h2 = new Host("123.4.2.2",8080);
    System.out.println("N1895639950=="+h1.getHashCode());
    System.out.println("P624319984=="+h2.getHashCode());
  }

}