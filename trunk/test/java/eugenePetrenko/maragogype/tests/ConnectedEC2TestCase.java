package eugenePetrenko.maragogype.tests;

import com.xerox.amazonws.ec2.Jec2;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 26.05.11 17:59
 */
public class ConnectedEC2TestCase {
  /**
   * Place amaz.on file to user home folder with Amazon EC2 access key.
   * This file should be a java properties file with keys:
   * 'access-id' and 'secret-key'
   *
   * @return connected Jec2
   */
  @NotNull
  public Jec2 createConnectedClient() {
    final Properties amazonAccessProperties;
    try {
      String userProfilePath = System.getenv("USERPROFILE");
      File userProfileDir = new File(userProfilePath);
      File amazonAccessPropertiesFile = new File(userProfileDir, ".amaz.on");
      amazonAccessProperties = new Properties();
      amazonAccessProperties.load(new FileInputStream(amazonAccessPropertiesFile));

      return new Jec2(
              amazonAccessProperties.getProperty("access-id"),
              amazonAccessProperties.getProperty("secret-key")
      );
    } catch (IOException e) {
      throw new RuntimeException("Could not read Amazon Access properties: "+e.getMessage(), e);
    }
  }

}
