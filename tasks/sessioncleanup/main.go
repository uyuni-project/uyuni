package main

import (
	"database/sql"
	"fmt"
	"log"
	"os"
	"strconv"
	"time"

	_ "github.com/lib/pq"
)

func getenv(key, fallback string) string {
    value := os.Getenv(key)
    if len(value) == 0 {
        return fallback
    }
    return value
}

func main() {
    var sessionLifetime int64 = 3600
    var err error
    if len(os.Args[1:]) == 1 {
        sessionLifetime, err = strconv.ParseInt(os.Args[1], 10, 64)
        if err != nil {
            log.Fatalf("Failed to parse %s as integer", os.Args[1])
        }
    }
    host := getenv("PGSQL_HOST", "localhost")
    port := getenv("PGSQL_PORT", "5432")
    user := os.Getenv("PGSQL_USER")
    password := os.Getenv("PGSQL_PASSWORD")
    dbname := getenv("PGSQL_DB", "spacewalk")
    connStr := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable", host, port, user, password, dbname)
    db, err := sql.Open("postgres", connStr)
    if err != nil {
        log.Fatalf("Failed to connect to DB: %s", err)
    }

    bound := time.Now().Unix() - 2 * sessionLifetime

    result, err := db.Exec("delete from PXTSessions where expires < $1", bound)
    if err != nil {
        log.Fatalf("Failed executing query: %s", err)
    }
    
    sessionsDeleted, err := result.RowsAffected()
    if err != nil {
        log.Fatalf("Failed to get affected rows: %s", err)
    }

    if sessionsDeleted > 0 {
        log.Printf("%d stale session(s) deleted", sessionsDeleted)
    } else {
        log.Print("No stale session deleted")
    }
}
