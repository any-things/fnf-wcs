package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

/**
 * 경광등 마스터
 * 
 * @author shortstop
 */
@Table(name = "tower_lamp", idStrategy = GenerationRule.UUID, uniqueFields="domainId,ipAddress", indexes = {
	@Index(name = "ix_tower_lamp_0", columnList = "domain_id,ip_address", unique = true)
})
public class TowerLamp extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 581542685820091129L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "tower_lamp_cd", nullable = false, length = 20)
	private String towerLampCd;
	
	@Column (name = "ip_address", nullable = false, length = 20)
	private String ipAddress;

	@Column (name = "port", nullable = false)
	private Integer port;

	@Column (name = "status", length = 20)
	private String status;

	@Column (name = "lamp_r", length = 5)
	private String lampR;

	@Column (name = "lamp_g", length = 5)
	private String lampG;

	@Column (name = "lamp_a", length = 5)
	private String lampA;

	@Column (name = "sound_ch", length = 1)
	private String soundCh;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTowerLampCd() {
		return towerLampCd;
	}

	public void setTowerLampCd(String towerLampCd) {
		this.towerLampCd = towerLampCd;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLampR() {
		return lampR;
	}

	public void setLampR(String lampR) {
		this.lampR = lampR;
	}

	public String getLampG() {
		return lampG;
	}

	public void setLampG(String lampG) {
		this.lampG = lampG;
	}

	public String getLampA() {
		return lampA;
	}

	public void setLampA(String lampA) {
		this.lampA = lampA;
	}

	public String getSoundCh() {
		return soundCh;
	}

	public void setSoundCh(String soundCh) {
		this.soundCh = soundCh;
	}

}
