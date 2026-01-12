package com.rest.web_app_gestion_reservation.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.util.JpaUtil;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {

    // --- Authentication & User Management ---

    public User authenticate(String username, String password) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            List<User> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public User registerUser(String username, String email, String password, String fullName, boolean admin) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = new User(username, email, password, fullName, admin);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    // --- Room Management ---

    public List<Room> listAllRooms() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery("SELECT r FROM Room r ORDER BY r.name", Room.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Room saveRoom(Room room) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (room.getId() == null) {
                em.persist(room);
            } else {
                room = em.merge(room);
            }
            em.getTransaction().commit();
            return room;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    public boolean deleteRoom(long roomId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Room room = em.find(Room.class, roomId);
            if (room != null) {
                // Delete associated reservations first
                em.createQuery("DELETE FROM Reservation r WHERE r.room.id = :roomId")
                        .setParameter("roomId", roomId)
                        .executeUpdate();
                em.remove(room);
                em.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    // --- Reservation Management ---

    public List<Reservation> listAllReservations() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room ORDER BY r.startDateTime DESC",
                    Reservation.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> listReservationsForUser(long userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.room WHERE r.user.id = :userId ORDER BY r.startDateTime DESC",
                    Reservation.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean isRoomAvailable(long roomId, LocalDateTime start, LocalDateTime end, Long excludeReservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(r) FROM Reservation r ")
                    .append("WHERE r.room.id = :roomId ")
                    .append("AND r.startDateTime < :end ")
                    .append("AND r.endDateTime > :start");

            if (excludeReservationId != null) {
                jpql.append(" AND r.id != :excludeId");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("roomId", roomId);
            query.setParameter("start", start);
            query.setParameter("end", end);
            if (excludeReservationId != null) {
                query.setParameter("excludeId", excludeReservationId);
            }

            return query.getSingleResult() == 0;
        } finally {
            em.close();
        }
    }

    public Reservation createReservation(long userId, long roomId, LocalDateTime start, LocalDateTime end) {
        if (!isRoomAvailable(roomId, start, end, null)) {
            return null;
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, userId);
            Room room = em.find(Room.class, roomId);
            if (user == null || room == null) {
                em.getTransaction().rollback();
                return null;
            }

            Reservation reservation = new Reservation(user, room, start, end);
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    public Reservation updateReservation(long reservationId, long roomId, LocalDateTime start, LocalDateTime end) {
        if (!isRoomAvailable(roomId, start, end, reservationId)) {
            return null;
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            Room room = em.find(Room.class, roomId);
            if (reservation == null || room == null) {
                em.getTransaction().rollback();
                return null;
            }

            reservation.setRoom(room);
            reservation.setStartDateTime(start);
            reservation.setEndDateTime(end);

            reservation = em.merge(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    public boolean cancelReservation(long reservationId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                em.remove(reservation);
                em.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Room> findAlternativeRooms(LocalDateTime start, LocalDateTime end) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                    "SELECT r FROM Room r WHERE r.id NOT IN (" +
                            "  SELECT res.room.id FROM Reservation res " +
                            "  WHERE res.startDateTime < :end AND res.endDateTime > :start" +
                            ") ORDER BY r.capacity DESC",
                    Room.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            query.setMaxResults(3);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
