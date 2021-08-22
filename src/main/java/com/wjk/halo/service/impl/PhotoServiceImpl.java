package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.PhotoDTO;
import com.wjk.halo.model.entity.Photo;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.PhotoParam;
import com.wjk.halo.model.params.PhotoQuery;
import com.wjk.halo.repository.PhotoRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.PhotoService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhotoServiceImpl extends AbstractCrudService<Photo, Integer> implements PhotoService {

    private final PhotoRepository photoRepository;

    public PhotoServiceImpl(PhotoRepository photoRepository) {
        super(photoRepository);
        this.photoRepository = photoRepository;
    }

    @Override
    public List<PhotoDTO> listDtos(Sort sort) {
        return listAll(sort).stream().map(photo -> (PhotoDTO) new PhotoDTO().convertFrom(photo)).collect(Collectors.toList());
    }

    @Override
    public Page<PhotoDTO> pageDtosBy(Pageable pageable, PhotoQuery photoQuery) {
        Page<Photo> photoPage = photoRepository.findAll(buildSpecByQuery(photoQuery), pageable);

        return photoPage.map(photo -> new PhotoDTO().convertFrom(photo));
    }

    @Override
    public Photo createBy(PhotoParam photoParam) {
        return create(photoParam.convertTo());
    }

    @Override
    public List<String> listAllTeams() {
        return photoRepository.findAllTeams();
    }

    @NonNull
    private Specification<Photo> buildSpecByQuery(@NonNull PhotoQuery photoQuery){
        return (Specification<Photo>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (photoQuery.getTeam() != null){
                predicates.add(criteriaBuilder.equal(root.get("team"), photoQuery.getTeam()));
            }

            if (photoQuery.getKeyword() != null){
                String likeCondition = String.format("%%%s%%", StringUtils.strip(photoQuery.getKeyword()));
                Predicate nameLike = criteriaBuilder.like(root.get("name"), likeCondition);
                Predicate descriptionLike = criteriaBuilder.like(root.get("description"), likeCondition);
                Predicate locationLike = criteriaBuilder.like(root.get("location"), likeCondition);

                predicates.add(criteriaBuilder.or(nameLike, descriptionLike, locationLike));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }


}
