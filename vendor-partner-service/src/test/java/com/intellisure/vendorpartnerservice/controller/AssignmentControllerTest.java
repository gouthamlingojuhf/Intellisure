package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.*;
import com.intellisure.vendorpartnerservice.service.VendorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {
    @Mock VendorService service; @InjectMocks AssignmentController controller;

    @Test
    void delegatesCreateAcceptStatusAndComplete() {
        UUID vendor = UUID.randomUUID(), claim = UUID.randomUUID(), id = UUID.randomUUID();
        AssignmentResponse response = new AssignmentResponse(id, vendor, claim, "tow", "ASSIGNED", null, null, null, null, null);
        when(service.assign(vendor, claim, "tow")).thenReturn(Mono.just(response));
        when(service.updateAssignment(id, "ACCEPTED")).thenReturn(Mono.just(response));
        when(service.updateAssignment(id, "REVIEW")).thenReturn(Mono.just(response));
        when(service.updateAssignment(id, "COMPLETED")).thenReturn(Mono.just(response));
        assertSame(response, controller.create(new CreateAssignmentRequest(vendor, claim, "tow")).block());
        assertSame(response, controller.accept(id).block());
        assertSame(response, controller.status(id, "REVIEW").block());
        assertSame(response, controller.complete(id).block());
        verify(service).assign(vendor, claim, "tow");
        verify(service).updateAssignment(id, "COMPLETED");
    }
}
