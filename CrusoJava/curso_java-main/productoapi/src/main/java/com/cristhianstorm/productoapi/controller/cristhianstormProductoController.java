package com.cristhianstorm.productoapi.controller;

import com.cristhianstorm.productoapi.model.Producto;
import com.cristhianstorm.productoapi.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("api/productos")
public class cristhianstormProductoController {

    @Autowired
    private ProductoRepository productoRepo;

    @Value("${app.nombre}")
    private String nombreAplicacion;

    @GetMapping("/mensaje")
    public ResponseEntity<?> mostrarMensaje(@RequestHeader(name = "apikey", defaultValue = "") String apikey) {
        if ("cristhianKey123".equals(apikey)) {
            return ResponseEntity.ok("Bienvenido a " + nombreAplicacion);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado: API Key inválida.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> lista = productoRepo.findAll();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0 || producto.getStock() < 0) {
            return ResponseEntity.badRequest().body("El precio debe ser mayor a 0 y el stock no puede ser negativo.");
        }

        if (productoRepo.findById(producto.getCodigo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un producto con el código: " + producto.getCodigo());
        }

        producto.setEstado(producto.getStock() > 0 ? "Disponible" : "Sin stock");
        productoRepo.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> buscarProductoPorCodigo(@PathVariable Integer codigo) {
        Optional<Producto> productoOpt = productoRepo.findById(codigo);
        if (productoOpt.isPresent()) {
            return ResponseEntity.ok(productoOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el producto con código: " + codigo);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        Optional<Producto> productoOpt = productoRepo.findById(id);
        if (productoOpt.isPresent()) {
            productoRepo.deleteById(id);
            return ResponseEntity.ok("Producto eliminado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el producto con código: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody Producto nuevoProducto) {
        if (nuevoProducto.getPrecio().compareTo(BigDecimal.ZERO) <= 0 || nuevoProducto.getStock() < 0) {
            return ResponseEntity.badRequest().body("Precio inválido o stock negativo.");
        }

        Optional<Producto> productoOpt = productoRepo.findById(id);
        if (productoOpt.isPresent()) {
            Producto p = productoOpt.get();
            p.setNombre(nuevoProducto.getNombre());
            p.setDescripcion(nuevoProducto.getDescripcion());
            p.setStock(nuevoProducto.getStock());
            p.setPrecio(nuevoProducto.getPrecio());
            p.setEstado(nuevoProducto.getStock() > 0 ? "Disponible" : "Sin stock");
            productoRepo.save(p);
            return ResponseEntity.ok(p);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto con código " + id + " no encontrado.");
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> generarEstadisticas() {
        List<Producto> productos = productoRepo.findAll();
        if (productos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos disponibles para estadísticas.");
        }

        int total = productos.size();
        BigDecimal sumaPrecios = productos.stream()
                .map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = sumaPrecios.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        long disponibles = productos.stream().filter(p -> "Disponible".equalsIgnoreCase(p.getEstado())).count();
        long sinStock = total - disponibles;

        Map<String, Object> estadisticas = new LinkedHashMap<>();
        estadisticas.put("totalProductos", total);
        estadisticas.put("precioPromedio", promedio);
        estadisticas.put("productosDisponibles", disponibles);
        estadisticas.put("productosSinStock", sinStock);

        return ResponseEntity.ok(estadisticas);
    }
} 
