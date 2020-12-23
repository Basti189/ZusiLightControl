package de.wolfsline.zusi.lightcontrol;

import de.wolf.Zusi3Schnittstelle.Zusi3Schnittstelle;
import de.wolf.Zusi3Schnittstelle.Interfaces.ZusiData;
import de.wolf.Zusi3Schnittstelle.Interfaces.ZusiEvent;
import de.wolf.Zusi3Schnittstelle.Values.FstAnz;
import de.wolfsline.de.config.Config;
import de.wolfsline.hue.bridge.HueBridge;

public class ZusiLightControl {
	
	private Zusi3Schnittstelle zusi;
	private HueBridge bridge;
	private Config config;

	public static void main(String[] args) {
		System.out.println("Starte ZusiLightControl");
		new ZusiLightControl().start();
	}
	
	public void start() {
		
		config = new Config();
		
		zusi = new Zusi3Schnittstelle(config.getString(Config.ZUSI_IP), config.getInteger(Config.ZUSI_PORT), "ZusiLightControl");
		zusi.register(this);
		zusi.setDebugOutput(true);
		zusi.requestFuehrerstandsbedienung(false);
		zusi.requestProgrammdaten(false);
		zusi.reqFstAnz(FstAnz.Aussenhelligkeit);
		zusi.connect();
		
		bridge = new HueBridge("ZusiLightControl", "3.4.1.0");
		bridge.setDebugOutput(false);
		bridge.discoverNUPNP(); // called only, when ip is not saved
		bridge.requestToken(); // called only, when token is not saved
		bridge.getAllGroups();
	}
	
	@ZusiEvent(0x00)
	public void onConnectionChanged(int status, int count) {
		if (status == 0) {
			System.out.println("Verbindung getrennt");
		} else if (status == 1) {
			System.out.println("Verbunden");
		} else if (status == 2) {
			System.out.println("Verbindung zum Server verloren");
		} else if (status == 3) {
			System.out.println("Verbindungsversuch " + count);
		}
	}
	
	@ZusiEvent(0x10)
	public void onConnectionCreated(String version, String verbindungsinfo, boolean client_aktzeptiert) {
		System.out.println("Zusi-Version: " + version);
		System.out.println("Zusi-Verbindungsinfo: " + verbindungsinfo);
		System.out.println("Client ok? " + client_aktzeptiert);
	}
	
	@ZusiData(FstAnz.Aussenhelligkeit)
	public void onBrightnessChanged(int lightning) {
		lightning /= 2;
		System.out.println("Helligkeit: " + lightning + "%");
		int lightForHue = (int) (lightning*2.55D);
		if (lightForHue < 10) {
			lightForHue = 10;
		}
		bridge.setGroupBrightness(bridge.group.get(config.getString(Config.ZUSI_HUE_GROUP)), lightForHue);
	}
}
