package com.npes87184.s2tdroid.donate.model;

import junit.framework.TestCase;

public class AnalysisTest extends TestCase {

    String strSimple = "hello! 龙傲天";
    String strTraditional = "hello! 龍傲天";

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
    }

    public void testStoT() {
        assertTrue("Test StoT failed.", strTraditional.equals(Analysis.StoT(strSimple)));
    }

    public void testTtoS() {
        assertTrue("Test TtoS failed.", strSimple.equals(Analysis.TtoS(strTraditional)));
    }

    public void testIsTraditional() {
        assertTrue("Test IsTraditional failed.", Analysis.isTraditional(strTraditional) > 0);
        assertTrue("Test IsTraditional failed.", Analysis.isTraditional(strSimple) <= 0);
    }
}