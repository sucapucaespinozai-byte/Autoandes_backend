package pe.edu.upeu.autoandes.service.servicelmpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.service.ClienteService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServicelmpl implements ClienteService {
  private final ClienteRepository repository;
  private final AutoAndesMapper mapper;

  public java.util.List<ClienteResponseDTO> listar() {
    return repository.findAll(org.springframework.data.domain.Sort.by("nombres")).stream()
        .map(mapper::cliente)
        .toList();
  }
}
