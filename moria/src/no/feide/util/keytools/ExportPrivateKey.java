package no.feide.util.keytools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import sun.misc.BASE64Encoder;

/**
 * 
 * This class can be used to extract the private key part
 * of certficates in JKS keystores
 *
 * @author Bjørn Ola Smievoll <b.o.smievoll@usit.uio.no>
 *
 * @version $Id$ 
 * 
 */
public class ExportPrivateKey {

	public static void main(String[] args) {

		Option keyStoreFile =
			OptionBuilder
				.withArgName("keystore")
				.isRequired()
				.hasArg()
				.withDescription("keystore file")
				.withLongOpt("keystore")
				.create("s");

		Option keyStorePassPhrase =
			OptionBuilder
				.withArgName("passphrase")
				.isRequired()
				.hasArg()
				.withDescription("keystore passphrase")
				.withLongOpt("passphrase")
				.create("p");

		Option certAlias =
			OptionBuilder
				.withArgName("alias")
				.isRequired()
				.hasArg()
				.withDescription("certificate alias")
				.withLongOpt("alias")
				.create("a");

		Option privKeyFile =
			OptionBuilder
				.withArgName("private key")
				.isRequired()
				.hasArg()
				.withDescription("private key file")
				.withLongOpt("privkey")
				.create("k");

		CommandLineParser parser = new PosixParser();
		Options options = new Options();

		options.addOption(keyStoreFile);
		options.addOption(keyStorePassPhrase);
		options.addOption(certAlias);
		options.addOption(privKeyFile);

		try {
			CommandLine line = parser.parse(options, args);
			doit(
				line.getOptionValue("s"),
				line.getOptionValue("p"),
				line.getOptionValue("a"),
				line.getOptionValue("k"));
		} catch (ParseException pe) {
			System.err.println("Parsing failed.  Reason: " + pe.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ExportPrivateKey", options);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	static private void doit(String keyStoreFileName, String keyStorePP, String alias, String privKeyFileName)
		throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keyStoreFileName), keyStorePP.toCharArray());

		KeyPair keyPair = getPrivateKey(keyStore, alias, keyStorePP);

		PrivateKey privKey = keyPair.getPrivate();

		BASE64Encoder b64Enc = new BASE64Encoder();

		String ls = System.getProperty("line.separator");

		String cert =
			"-----BEGIN PRIVATE KEY-----"
				+ ls
				+ b64Enc.encode(privKey.getEncoded())
				+ ls
				+ "-----END PRIVATE KEY-----"
				+ ls;

		FileOutputStream out = new FileOutputStream(privKeyFileName);
		out.write(cert.getBytes());
		out.close();
	}

	static private KeyPair getPrivateKey(KeyStore keystore, String alias, String password) throws Exception {
		Key key = keystore.getKey(alias, password.toCharArray());
		if (key instanceof PrivateKey) {
			Certificate cert = keystore.getCertificate(alias);
			PublicKey publicKey = cert.getPublicKey();
			return new KeyPair(publicKey, (PrivateKey) key);
		}
		return null;
	}
}
