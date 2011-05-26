package eugenePetrenko.maragogype.tests;

import com.xerox.amazonws.ec2.*;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 26.05.11 19:13
 */
public class Jet2Test extends ConnectedEC2TestCase {

  @Test
  public void testDescribeSecurityGroups() throws EC2Exception {
    final Jec2 cli = createConnectedClient();

    cli.setRegionUrl(RegionInfo.REGIONURL_EU_WEST);

    final List<GroupDescription> g = cli.describeSecurityGroups();

    for (GroupDescription gr : g) {
      System.out.println("gr = " + gr);
    }
  }

  @Test
  public void testDescribeInstnces() throws EC2Exception {
    final Jec2 cli = createConnectedClient();

    cli.setRegionUrl(RegionInfo.REGIONURL_EU_WEST);

    final List<ReservationDescription> g = cli.describeInstances();

    for (ReservationDescription i : g) {
      for (ReservationDescription.Instance z : i.getInstances()) {
        System.out.println("z = " + z);
      }
    }
  }

}
