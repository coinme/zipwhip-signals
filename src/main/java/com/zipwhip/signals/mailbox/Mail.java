package com.zipwhip.signals.mailbox;

/**
 * Date: 5/14/13
 * Time: 2:11 PM
 *
 * @author Michael
 * @version 1
 */
public class Mail {

    private String content;
    private long version;

    public Mail(String content, long version) {
        this.content = content;
        this.version = version;
    }

    public Mail() {

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mail)) return false;

        Mail mail = (Mail) o;

        if (version != mail.version) return false;
        if (content != null ? !content.equals(mail.content) : mail.content != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }
}
