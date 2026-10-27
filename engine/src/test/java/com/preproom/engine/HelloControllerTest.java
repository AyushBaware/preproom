package com.preproom.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

    @Test
    void helloReturnsServiceNameAndOkStatus() {
        HelloController controller = new HelloController();

        var response = controller.hello();

        assertThat(response).containsEntry("service", "engine");
        assertThat(response).containsEntry("status", "ok");
    }
}