package controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import services.RequestDAO;

import static org.junit.Assert.*;

public class ReimbursementRequestControllerTest {
    private ReimbursementRequestController testController;
    private RequestDAO testDao;

    @Before
    public void setUp() throws Exception {
        testDao = Mockito.mock(RequestDAO.class);
        testController = new ReimbursementRequestController(testDao);
    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testValidateMoney() {
        String[] valid = new String[]{"123.23", "0.01", "1", "0.2", "897.0000000", "0.0"};
        long[] amounts = new long[]{12323, 1, 100, 20, 89700, 0};
        String[] invalid = new String[]{"123.345", "-12", "999999999999999999", "123a", "123.23."};

        for (int i = 0; i < valid.length; ++i)
            assertEquals(amounts[i], testController.validateMoney(valid[i]));

        for (String amount : invalid)
            assertThrows(RuntimeException.class, () -> testController.validateMoney(amount));
    }

    @Test
    public void testSubmitNewRequest() {
        Mockito.when(testDao.addRequest(Mockito.any())).thenReturn(-12);
        assertEquals(-1, testController.submitNewRequest(1, "123", "", ""));
        assertThrows(RuntimeException.class, () -> testController.submitNewRequest(1, "-12", "", ""));
    }

    @Test
    public void testResolveRequest() {
        Mockito.when(testDao.resolveRequest(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        assertTrue(testController.resolveRequest(1, 1, true));
    }
}