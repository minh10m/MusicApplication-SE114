package com.music.application.be.modules.comment;

import com.music.application.be.modules.comment.dto.CommentActionResponseDTO;
import com.music.application.be.modules.comment.dto.CommentResponseDTO;
import com.music.application.be.modules.comment.dto.CreateCommentDTO;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CommentResponseDTO createComment(Long songId, CreateCommentDTO createCommentDTO) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + songId));
        User user = userRepository.findById(createCommentDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + createCommentDTO.getUserId()));

        Comment comment = new Comment();
        comment.setSong(song);
        comment.setUser(user);
        comment.setContent(createCommentDTO.getContent());

        if (createCommentDTO.getParentId() != null) {
            Comment parent = commentRepository.findById(createCommentDTO.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);
        return mapToResponseDTOWithReplies(comment);
    }

    @Transactional
    public CommentActionResponseDTO likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        CommentActionResponseDTO responseDTO = new CommentActionResponseDTO();

        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            commentLikeRepository.save(like);
            comment.setLikes(comment.getLikes() + 1);
            commentRepository.save(comment);

            responseDTO.setSuccess(true);
            responseDTO.setMessage("Comment liked successfully");
        } else {
            responseDTO.setSuccess(false);
            responseDTO.setMessage("User has already liked this comment");
        }

        responseDTO.setComment(mapToResponseDTOWithReplies(comment));
        return responseDTO;
    }

    @Transactional
    public CommentActionResponseDTO unlikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        CommentActionResponseDTO responseDTO = new CommentActionResponseDTO();

        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
            comment.setLikes(comment.getLikes() - 1);
            if (comment.getLikes() < 0) comment.setLikes(0L); // Đảm bảo không âm
            commentRepository.save(comment);

            responseDTO.setSuccess(true);
            responseDTO.setMessage("Comment unliked successfully");
        } else {
            responseDTO.setSuccess(false);
            responseDTO.setMessage("User has not liked this comment");
        }

        responseDTO.setComment(mapToResponseDTOWithReplies(comment));
        return responseDTO;
    }

    public List<CommentResponseDTO> getCommentsBySongId(Long songId) {
        List<Comment> comments = commentRepository.findBySongIdAndParentIsNull(songId);
        return comments.stream()
                .map(this::mapToResponseDTOWithReplies)
                .collect(Collectors.toList());
    }

    public List<Long> getUserLikedComments(Long songId, Long userId) {
        return commentLikeRepository.findCommentIdsByUserIdAndSongId(userId, songId);
    }

    private CommentResponseDTO mapToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setSongId(comment.getSong().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setLikes(comment.getLikes());
        return dto;
    }

    private CommentResponseDTO mapToResponseDTOWithReplies(Comment comment) {
        CommentResponseDTO dto = mapToResponseDTO(comment);
        List<Comment> replies = commentRepository.findByParentId(comment.getId());
        dto.setReplies(replies.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList()));
        return dto;
    }
}