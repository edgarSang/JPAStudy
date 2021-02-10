package jpabook.start;

public class MemberDAO {

    String sql = "INSERT INTO MEMBER(MEMBER_ID, NAME) VALUES(?,?)";

    public Member find(String memberId) {
        return new Member();
    }

    public void save(Member member) {

    }

    public Member findWithTeam(String memberId) {
        return new Member();
    }

}
