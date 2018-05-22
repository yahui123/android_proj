package de.bitsharesmunich.graphenej;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by nelson on 12/16/16.
 */
public class PublicKeyTest {
    @Before
    public void setUp() throws Exception {

    }
    @org.junit.Test
    public void equals() throws Exception {
        Address address1 = new Address("BDS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
        Address address2 = new Address("BDS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
        Address address3 = new Address("BDS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYp00");
        PublicKey pk1 = address1.getPublicKey();
        PublicKey pk2 = address2.getPublicKey();
        PublicKey pk3 = address3.getPublicKey();
        assertEquals("Public keys must be equal", pk1, pk2);
        assertNotEquals("Public keys must not be equal", pk1, pk3);
    }

    @After
    public void tearDown() throws Exception {

    }

}