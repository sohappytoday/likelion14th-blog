package likelion14th.blog.dto.request;

import likelion14th.blog.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {
    private String title;
    private String content;
    private String author;
    private String password;
}