package com.sang.health.repository.reply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.sang.health.entity.reply.ReReply;

@DataJpaTest
class ReReplyRepositoyTest {

	@Autowired
	private ReReplyRepositoy reReplyRepository;
	
	@Test
	void testFindByReplyidIn() {
		// given
		reReplyRepository.save(new ReReply("안녕", "User", 1L));
		reReplyRepository.save(new ReReply("안녕", "Admin", 2L));
		
		// when
		List<ReReply> result = reReplyRepository.findByReplyidIn(List.of(1L));
		
		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUsername()).isEqualTo("User");
		
	}

}
