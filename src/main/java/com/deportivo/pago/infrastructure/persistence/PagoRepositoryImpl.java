package com.deportivo.pago.infrastructure.persistence;

import com.deportivo.pago.domain.model.Pago;
import com.deportivo.pago.domain.repository.PagoRepository;
import com.deportivo.pago.infrastructure.mapper.PagoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class PagoRepositoryImpl implements PagoRepository {

    private final PagoPanacheRepository panacheRepository;
    private final PagoMapper pagoMapper;

    @Override
    public Optional<Pago> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(pagoMapper::toDomain);
    }

    @Override
    public List<Pago> findAll() {
        return pagoMapper.toDomainList(panacheRepository.listAll());
    }

    @Override
    public List<Pago> findByReservaId(Long reservaId) {
        return pagoMapper.toDomainList(panacheRepository.list("reservaId", reservaId));
    }

    @Override
    public Optional<Pago> findByTransaccionId(String transaccionId) {
        return panacheRepository.find("transaccionId", transaccionId)
                .firstResultOptional()
                .map(pagoMapper::toDomain);
    }

    @Override
    public Pago save(Pago pago) {
        PagoEntity entity = pagoMapper.toEntity(pago);
        if (entity.getId() == null) {
            panacheRepository.persist(entity);
        } else {
            entity = panacheRepository.getEntityManager().merge(entity);
        }
        return pagoMapper.toDomain(entity);
    }

    @Override
    public void delete(Long id) {
        panacheRepository.deleteById(id);
    }
}
