package com.eclipsoft.face2face.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.eclipsoft.face2face.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AgentDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentDTO.class);
        AgentDTO agentDTO1 = new AgentDTO();
        agentDTO1.setId("id1");
        AgentDTO agentDTO2 = new AgentDTO();
        assertThat(agentDTO1).isNotEqualTo(agentDTO2);
        agentDTO2.setId(agentDTO1.getId());
        assertThat(agentDTO1).isEqualTo(agentDTO2);
        agentDTO2.setId("id2");
        assertThat(agentDTO1).isNotEqualTo(agentDTO2);
        agentDTO1.setId(null);
        assertThat(agentDTO1).isNotEqualTo(agentDTO2);
    }
}
