package com.eclipsoft.face2face.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.eclipsoft.face2face.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Event.class);
        Event event1 = new Event();
        event1.setId("id1");
        Event event2 = new Event();
        event2.setId(event1.getId());
        assertThat(event1).isEqualTo(event2);
        event2.setId("id2");
        assertThat(event1).isNotEqualTo(event2);
        event1.setId(null);
        assertThat(event1).isNotEqualTo(event2);
    }
}
