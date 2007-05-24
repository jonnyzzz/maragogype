
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

public class TestJec2 {
    private static Logger log = LoggingConfigurator.configureLogging(TestJec2.class);

	public static void main(String [] args) throws Exception {
		final String AWSAccessKeyId = "[AWS Access Id]";
		final String SecretAccessKey = "[AWS Secret Key]";

		Jec2 ec2 = new Jec2(AWSAccessKeyId, SecretAccessKey);
		List<String> params = new ArrayList<String>();
	
/*
		for (int i=0; i<10; i++) {
			long start = System.currentTimeMillis();
*/
			//params.add("291944132575");
			List<ImageDescription> images = ec2.describeImages(params);
			log.info("Available Images");
			for (ImageDescription img : images) {
				if (img.getImageState().equals("available")) {
					log.info(img.getImageId()+"\t"+img.getImageLocation()+"\t"+img.getImageOwnerId());
				}
			}
/*
			long end = System.currentTimeMillis();
			log.info("duration to find "+images.size()+" images = "+((end-start)/1000.0));
		}
*/

//		ec2.runInstances("ami-20b65349", 1, 1, new ArrayList<String>(), null, "xrxdak-keypair");

/*
*/
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
/*
*/
		ConsoleOutput consOutput = ec2.getConsoleOutput(instanceId);
		log.info("Console Output:");
		log.info(consOutput.getOutput());

		// test keypair methods
/*
		List<KeyPairInfo> info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.getKeyName()+", "+i.getKeyFingerprint());
		}
		ec2.createKeyPair("test-keypair");
		info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.getKeyName()+", "+i.getKeyFingerprint());
		}
		ec2.deleteKeyPair("test-keypair");
		info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.getKeyName()+", "+i.getKeyFingerprint());
		}
*/

		// test security group methods
		List<GroupDescription> info = ec2.describeSecurityGroups(new String [] {});
		log.info("SecurityGroup list");
		for (GroupDescription i : info) {
			log.info("group : "+i.getName()+", "+i.getDescription()+", "+i.getOwner());
		}
/*
*/
		ec2.createSecurityGroup("test-group", "My test security group");
		info = ec2.describeSecurityGroups(new String [] {});
		log.info("SecurityGroup list");
		for (GroupDescription i : info) {
			log.info("group : "+i.getName()+", "+i.getDescription());
		}
		ec2.authorizeSecurityGroupIngress("default", "tcp", 1000, 1001, "0.0.0.0/0");
		ec2.revokeSecurityGroupIngress("default", "tcp", 1000, 1001, "0.0.0.0/0");
		ec2.authorizeSecurityGroupIngress("default", "tcp", 1000, 1001, "0.0.0.0/0");
		ec2.revokeSecurityGroupIngress("default", "tcp", 1000, 1001, "0.0.0.0/0");

		ec2.authorizeSecurityGroupIngress("default", "test-group", "291944132575");
		ec2.revokeSecurityGroupIngress("default", "test-group", "291944132575");

		ec2.deleteSecurityGroup("test-group");
		info = ec2.describeSecurityGroups(new String [] {});
		log.info("GroupDescription list");
		for (GroupDescription i : info) {
			log.info("group : "+i.getName()+", "+i.getDescription());
		}

		// test image attribute methods
/*
*/
		DescribeImageAttributeResult res = ec2.describeImageAttribute("ami-11816478", ImageAttributeType.launchPermission);
		Iterator<ImageListAttributeItem> iter = res.getImageListAttribute().getImageListAttributeItems().iterator();
		log.info("image attrs");
		while (iter.hasNext()) {
			ImageListAttributeItem item = iter.next();
			log.info("image : "+res.getImageId()+", "+item.getValue());
		}
		LaunchPermissionAttribute attr = new LaunchPermissionAttribute();
		attr.getImageListAttributeItems().add(new ImageListAttributeItem(ImageListAttributeItemType.userId, "291944132575"));
		ec2.modifyImageAttribute("ami-11816478", attr, ImageListAttributeOperationType.add);
		res = ec2.describeImageAttribute("ami-11816478", ImageAttributeType.launchPermission);
		iter = res.getImageListAttribute().getImageListAttributeItems().iterator();
		log.info("image attrs");
		while (iter.hasNext()) {
			ImageListAttributeItem item = iter.next();
			log.info("image : "+res.getImageId()+", "+item.getValue());
		}
		ec2.resetImageAttribute("ami-11816478", ImageAttributeType.launchPermission);
		res = ec2.describeImageAttribute("ami-11816478", ImageAttributeType.launchPermission);
		iter = res.getImageListAttribute().getImageListAttributeItems().iterator();
		log.info("image attrs");
		while (iter.hasNext()) {
			ImageListAttributeItem item = iter.next();
			log.info("image : "+res.getImageId()+", "+item.getValue());
		}
	}
}

