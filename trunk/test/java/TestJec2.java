
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import com.xerox.amazonws.tools.LoggingConfigurator;

public class TestJec2 {
    private static Logger log = LoggingConfigurator.configureLogging(TestJec2.class);

	public static void main(String [] args) throws Exception {
		Jec2 ec2 = new Jec2("1SEQ6QDW2YNW8T6K64R2", "7P1KY+a4FTtiVBuU935NHHOI19eYrbyWG7CDklmk", false, "localhost");
		List<String> params = new ArrayList<String>();
		params.add("291944132575");
//		List<ImageDescription> images = ec2.describeImagesByExecutability(params);
//		log.info("Available Images");
//		for (ImageDescription img : images) {
//			log.info(img.imageId+"\t"+img.imageLocation+"\t"+img.imageOwnerId);
//		}

		params = new ArrayList<String>();
		List<ReservationDescription> instances = ec2.describeInstances(params);
		log.info("Instances");
		String instanceId = "";
		for (ReservationDescription res : instances) {
			log.info(res.owner+"\t"+res.resId+"\t"+res.owner);
			if (res.instances != null) {
				for (Instance inst : res.instances) {
					log.info("\t"+inst.imageId+"\t"+inst.dnsName+"\t"+inst.state+"\t"+inst.keyName);
					instanceId = inst.instanceId;
				}
			}
		}
		// test console output
//		ConsoleOutput consOutput = ec2.getConsoleOutput(instanceId);
//		log.info("Console Output:");
//		log.info(consOutput.output);

		// test keypair methods
//		List<KeyPairInfo> info = ec2.describeKeyPairs(new String [] {});
//		log.info("keypair list");
//		for (KeyPairInfo i : info) {
//			log.info("keypair : "+i.keyName+", "+i.keyFingerprint);
//		}
//		ec2.createKeyPair("test-keypair");
//		info = ec2.describeKeyPairs(new String [] {});
//		log.info("keypair list");
//		for (KeyPairInfo i : info) {
//			log.info("keypair : "+i.keyName+", "+i.keyFingerprint);
//		}
//		ec2.deleteKeyPair("test-keypair");
//		info = ec2.describeKeyPairs(new String [] {});
//		log.info("keypair list");
//		for (KeyPairInfo i : info) {
//			log.info("keypair : "+i.keyName+", "+i.keyFingerprint);
//		}
	}
}
