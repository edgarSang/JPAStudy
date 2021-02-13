package jpabook.start;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaWriteBehind {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpastart");

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        transaction.begin();

        Member m1 = new Member();
        Member m2 = new Member();

        m1.setId("m1");
        m2.setId("m2");

        // Transaction write-behind
        em.persist(m1);
        em.persist(m2);

        m1 = em.find(Member.class, "m1");
        m1.setAge(23);
        m1.setUsername("hi1");

        // persistence context flush
        transaction.commit();

    }

    public static void testDetach(EntityManager em) {
        EntityTransaction tx = em.getTransaction();

        Member member = new Member();
        member.setId("memberA");
        member.setUsername("회원A");

        em.persist(member);

        em.detach(member);

        tx.commit();
    }
}
