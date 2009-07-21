/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.tools.visualvm.api.caching.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Bachorik
 */
public class SoftReferenceExTest {

    public SoftReferenceExTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of equals method, of class SoftReferenceEx.
     */
    @Test
    public void testEqualsNonEmpty() {
        System.out.println("equals - non empty");
        SoftReferenceEx instance1 = new SoftReferenceEx("xxx");
        SoftReferenceEx instance2 = new SoftReferenceEx("xxx");
        // equals must yield the equals result of the referrents
        assertEquals(instance2, instance1);
        // also, the hashcodes must be the same for equaling references
        assertEquals(instance2.hashCode(), instance1.hashCode());
    }

    /**
     * Test of equals method, of class SoftReferenceEx.
     */
    @Test
    public void testEqualsEmpty() {
        System.out.println("equals - empty");
        SoftReferenceEx instance1 = new SoftReferenceEx("xxx");
        SoftReferenceEx instance2 = new SoftReferenceEx(null);
        SoftReferenceEx instance3 = new SoftReferenceEx(null);
        // equals for "non-null"x"null" must yield FALSE
        assertFalse(instance2.equals(instance1));
        // equals for "null"x"null" must yield FALSE
        assertFalse(instance2.equals(instance3));
    }

    @Test
    public void testNotEqualsNonEmpty() {
        System.out.println("equals - non empty");
        SoftReferenceEx instance1 = new SoftReferenceEx("xxx");
        SoftReferenceEx instance2 = new SoftReferenceEx("yyy");
        // equals must yield the equals result of the referrents
        assertFalse(instance2.equals(instance1));
        // also, the hashcodes must not be the same for non-equaling references
        assertFalse(instance2.hashCode() == instance1.hashCode());
    }
}