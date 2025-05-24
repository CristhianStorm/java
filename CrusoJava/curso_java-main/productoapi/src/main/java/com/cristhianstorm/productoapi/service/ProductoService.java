package com.cristhianstorm.productoapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cristhianstorm.productoapi.model.Producto;
import com.cristhianstorm.productoapi.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;

    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return repository.findById(id);
    }

    public Producto guardarProducto(Producto p) {
        // Validaciones básicas:
        if (p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (p.getStock() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        // Setear estado basado en stock con operador ternario
        p.setEstado(p.getStock() > 0 ? "Disponible" : "Agotado");

        return repository.save(p);
    }

    public Producto actualizarProducto(Integer id, Producto p) {
        Optional<Producto> existente = repository.findById(id);
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el producto con el código " + id);
        }
        Producto prod = existente.get();

        if (p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (p.getStock() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        prod.setNombre(p.getNombre());
        prod.setDescripcion(p.getDescripcion());
        prod.setStock(p.getStock());
        prod.setPrecio(p.getPrecio());
        prod.setEstado(p.getStock() > 0 ? "Disponible" : "Agotado");

        return repository.save(prod);
    }

    public void eliminarProducto(Integer id) {
        repository.deleteById(id);
    }
}
