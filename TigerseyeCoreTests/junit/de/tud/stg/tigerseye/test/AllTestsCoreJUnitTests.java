package de.tud.stg.tigerseye.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.tud.stg.popart.builder.transformers.FileTypeTest;
import de.tud.stg.tigerseye.eclipse.core.codegeneration.GrammarBuilderTest;
import de.tud.stg.tigerseye.eclipse.core.codegeneration.GrammarTest;
import de.tud.stg.tigerseye.eclipse.core.codegeneration.UnicodeLookupTableTest;
import de.tud.stg.tigerseye.eclipse.core.preferences.TransformerTableDialogTest;
import de.tud.stg.tigerseye.eclipse.core.runtime.DSLClasspathResolverTest;
import de.tud.stg.tigerseye.eclipse.core.runtime.FileHelperTest;
import de.tud.stg.tigerseye.eclipse.core.runtime.JDTClasspathResolverTest;
import de.tud.stg.tigerseye.eclipse.core.utils.DSLExtensionsExtractorTest;
import de.tud.stg.tigerseye.eclipse.core.utils.KeyWordsExtractorTest;
import de.tud.stg.tigerseye.eclipse.core.utils.OutputPathHandlerTest;

@RunWith(Suite.class)
@SuiteClasses({FileTypeTest.class, //
	TransformerTableDialogTest.class, //
	DSLExtensionsExtractorTest.class, //
	KeyWordsExtractorTest.class, //
	OutputPathHandlerTest.class, //
	FileHelperTest.class, //
	JDTClasspathResolverTest.class, //
	UnicodeLookupTableTest.class, //
	GrammarBuilderTest.class, //
	GrammarTest.class, //
	})
public class AllTestsCoreJUnitTests {

}
