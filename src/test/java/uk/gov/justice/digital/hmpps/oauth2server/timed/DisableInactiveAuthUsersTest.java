package uk.gov.justice.digital.hmpps.oauth2server.timed;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DisableInactiveAuthUsersTest {
    @Mock
    private DisableInactiveAuthUsersService service;
    @Mock
    private TelemetryClient telemetryClient;

    private DisableInactiveAuthUsers disableInactiveAuthUsers;

    @Before
    public void setUp() {
        disableInactiveAuthUsers = new DisableInactiveAuthUsers(service, telemetryClient);
    }

    @Test
    public void findAndDisableInactiveAuthUsers() {
        when(service.processInBatches()).thenReturn(0);
        disableInactiveAuthUsers.findAndDisableInactiveAuthUsers();
        verify(service).processInBatches();
    }
}