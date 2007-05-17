
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.DescribeImageAttributeResult;
import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.Jec2.ImageListAttributeOperationType;
import com.xerox.amazonws.ec2.ImageAttribute.ImageAttributeType;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.ImageListAttributeItem;
import com.xerox.amazonws.ec2.ImageListAttribute.ImageListAttributeItemType;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchPermissionAttribute;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import com.xerox.amazonws.tools.LoggingConfigurator;

public class Ec2Sample {
	private static Logger log = LoggingConfigurator.configureLogging(Ec2Sample.class);

	public static void main(String [] args) throws Exception {
		final String AWSAccessKeyId = "[AWS Access Id]";
		final String SecretAccessKey = "[AWS Secret Key]";

		Jec2 ec2 = new Jec2(AWSAccessKeyId, SecretAccessKey);
	
		// describe images
		List<String> params = new ArrayList<String>();
		List<ImageDescription> images = ec2.describeImages(params);
		log.info("Available Images");
		for (ImageDescription img : images) {
			if (img.getImageState().equals("available")) {
				log.info(img.getImageId()+"\t"+img.getImageLocation()+"\t"+img.getImageOwnerId());
			}
		}

		// describe instances
		params = new ArrayList<String>();
		List<ReservationDescription> instances = ec2.describeInstances(params);
		log.info("Instances");
		String instanceId = "";
		for (ReservationDescription res : instances) {
			log.info(res.getOwner()+"\t"+res.getReservationId());
			if (res.getInstances() != null) {
				for (Instance inst : res.getInstances()) {
					log.info("\t"+inst.getImageId()+"\t"+inst.getDnsName()+"\t"+inst.getState()+"\t"+inst.getKeyName());
					instanceId = inst.getInstanceId();
				}
			}
		}

		// test console output
		ConsoleOutput consOutput = ec2.getConsoleOutput(instanceId);
		log.info("Console Output:");
		log.info(consOutput.getOutput());

		// show keypairs
		List<KeyPairInfo> info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.getKeyName()+", "+i.getKeyFingerprint());
		}
	}
}

