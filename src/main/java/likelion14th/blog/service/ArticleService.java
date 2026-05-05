package likelion14th.blog.service;

import jakarta.persistence.EntityNotFoundException;
import likelion14th.blog.domain.Article;
import likelion14th.blog.dto.request.ArticleRequest;
import likelion14th.blog.dto.response.ArticleResponse;
import likelion14th.blog.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {
    // 의존성 주입
    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse addArticle(String title, String content, String author, String password) {
        // Article 객체 생성
        Article article = new Article(title, content, author, password);

        // Article객체를 JPA의 save() 를 사용하여 DB에 저장
        articleRepository.save(article);

        // Article 객체를 response DTO 생성하여 반환
        // response 클래스의 정작 팩토리 메서드 from()을 통해 API 응답객체 생성
        return ArticleResponse.from(article);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getArticles() {
        // Article 전체 객체 리스트 가져오기
        List<Article> articles = articleRepository.findAll();

        // ArticleResponse 리스트로 변경
        List<ArticleResponse> articleResponses = articles.stream()
                .map(ArticleResponse::from)
                .toList();

        return articleResponses;
    }

    @Transactional(readOnly = true)
    public ArticleResponse getOneArticle(Long id) {
        // 변수로 받은 id를 가지는 Article 가져오기
        Article article = articleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다."));

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, String title, String content){
        Article article = articleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다."));;

        article.setTitle(title);
        article.setContent(content);

        articleRepository.save(article);
        return ArticleResponse.from(article);
    }

    @Transactional
    public Void deleteArticle(Long id){
        articleRepository.deleteById(id);

        return null;
    }
}

