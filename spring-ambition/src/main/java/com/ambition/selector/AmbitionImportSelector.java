package com.ambition.selector;

import com.ambition.service.CService;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Elewin
 * @date 2020-04-30 2:29 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class AmbitionImportSelector implements ImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		return new String[]{CService.class.getName()};
	}

}
