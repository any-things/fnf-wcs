/* Copyright © HatioLab Inc. All rights reserved. */
package operato.fnf.wcs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * 샘플 Boot 모듈 정보 파일
 * 
 * @author yang
 */
@Component("operatoFnFWcsModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/operato-fnf-wcs.properties")
public class ModuleProperties implements IModuleProperties {

	/**
	 * 모듈명
	 */
	@Value("${operato.fnf.wcs.name}")
	private String name;

	/**
	 * 버전
	 */
	@Value("${operato.fnf.wcs.version}")
	private String version;

	/**
	 * Module Built Time
	 */
	@Value("${operato.fnf.wcs.built.at}")
	private String builtAt;

	/**
	 * 모듈 설명
	 */
	@Value("${operato.fnf.wcs.description}")
	private String description;

	/**
	 * 부모 모듈
	 */
	@Value("${operato.fnf.wcs.parentModule}")
	private String parentModule;

	/**
	 * 모듈 Scada Package
	 */
	@Value("${operato.fnf.wcs.basePackage}")
	private String basePackage;

	/**
	 * Scan Service Path
	 */
	@Value("${operato.fnf.wcs.scanServicePackage}")
	private String scanServicePackage;

	/**
	 * Scan Entity Path
	 */
	@Value("${operato.fnf.wcs.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * Project Name
	 * 
	 * @return
	 */
	@Value("${operato.fnf.wcs.projectName}")
	private String projectName;

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public String getBuiltAt() {
		return builtAt;
	}

	public String getDescription() {
		return this.description;
	}

	public String getParentModule() {
		return this.parentModule;
	}

	public String getBasePackage() {
		return this.basePackage;
	}

	public String getScanServicePackage() {
		return this.scanServicePackage;
	}

	public String getScanEntityPackage() {
		return this.scanEntityPackage;
	}

	public String getProjectName() {
		return this.projectName;
	}
	
	@Override
	public String toString() {
		return FormatUtil.toJsonString(this);
	}
}