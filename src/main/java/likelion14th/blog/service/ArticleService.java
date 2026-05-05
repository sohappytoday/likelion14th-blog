package likelion14th.blog.service;

import likelion14th.blog.domain.Article;
import likelion14th.blog.dto.request.ArticleRequest;
import likelion14th.blog.dto.response.ArticleResponse;
import likelion14th.blog.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {
    // 의존성 주입
    private final ArticleRepository articleRepository;

    public ArticleResponse addArticle(String title, String content, String author, String password) {
        // Article 객체 생성
        Article article = new Article(title, content, author, password);

        // Article객체를 JPA의 save() 를 사용하여 DB에 저장
        articleRepository.save(article);

        // Article 객체를 response DTO 생성하여 반환
        // response 클래스의 정작 팩토리 메서드 from()을 통해 API 응답객체 생성
        return ArticleResponse.from(article);
    }
}

