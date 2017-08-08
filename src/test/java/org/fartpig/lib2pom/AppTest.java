package org.fartpig.lib2pom;

import org.fartpig.lib2pom.constant.GlobalConst;
import org.fartpig.lib2pom.util.ToolException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public void testAppNormal() {
		App app = new App();
		String[] args = {};
		app.main(args);
	}

	public void testAppArgs1() {
		App app = new App();
		String[] args = { "-p", "D:\\workspace-my\\test-classes\\lib" };
		app.main(args);
	}

	public void testAppArgs2() {
		App app = new App();
		try {
			String[] args = { "-o" };
			app.main(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the outPutPomFileName", e.getMessage());
		}
	}

	public void testAppArgs3() {
		App app = new App();
		String[] args = { "-o", "D:\\workspace-my\\test-classes" };
		app.main(args);
	}

	public void testAppArgs4() {
		App app = new App();
		String[] args = { "--print" };
		app.main(args);
	}

	public void testAppArgs5() {
		App app = new App();
		String[] args = { "-p" };
		app.main(args);
	}

	public void testAppArgs6() {
		App app = new App();
		try {
			String[] args = { "--inflate" };
			app.main(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the inflateOutPath", e.getMessage());
		}
	}

	public void testAppArgs7() {
		App app = new App();
		try {
			String[] args = { "-if" };
			app.main(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the inflateOutPath", e.getMessage());
		}
	}

	public void testAppArgs8() {
		App app = new App();
		String[] args = { "--inflate", "D:\\workspace-my\\lib2pom\\target\\test-classes\\targetlib" };
		app.main(args);
	}

	public void testAppArgs9() {
		App app = new App();
		String[] args = { "-if", "D:\\workspace-my\\lib2pom\\target\\test-classes\\targetlib" };
		app.main(args);
	}

	public void testAppArgs10() {
		App app = new App();
		String[] args = { "-if", "D:\\workspace-my\\lib2pom\\target\\test-classes\\targetlib",
				"D:\\workspace-my\\lib2pom\\target\\test-classes\\lib" };
		app.main(args);
	}
}
