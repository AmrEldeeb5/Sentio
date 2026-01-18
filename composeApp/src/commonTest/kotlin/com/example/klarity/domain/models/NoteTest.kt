package com.example.klarity.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the Note domain model.
 */
class NoteTest {

    @Test
    fun `wordCount returns correct count for single words`() {
        val note = Note(
            title = "Test",
            content = "Hello world test",
            folderId = null
        )
        assertEquals(3, note.wordCount())
    }

    @Test
    fun `wordCount handles empty content`() {
        val note = Note(
            title = "Test",
            content = "",
            folderId = null
        )
        assertEquals(0, note.wordCount())
    }

    @Test
    fun `wordCount handles multiple whitespace`() {
        val note = Note(
            title = "Test",
            content = "Hello   world\n\ntest",
            folderId = null
        )
        assertEquals(3, note.wordCount())
    }

    @Test
    fun `preview truncates long content`() {
        val longContent = "A".repeat(300)
        val note = Note(
            title = "Test",
            content = longContent,
            folderId = null
        )

        val preview = note.preview()
        assertTrue(preview.length <= 203) // 200 chars + "..."
        assertTrue(preview.endsWith("..."))
    }

    @Test
    fun `preview does not truncate short content`() {
        val note = Note(
            title = "Test",
            content = "Short content",
            folderId = null
        )

        val preview = note.preview()
        assertEquals("Short content", preview)
        assertFalse(preview.endsWith("..."))
    }

    @Test
    fun `matchesQuery finds match in title`() {
        val note = Note(
            title = "My Important Note",
            content = "Some content",
            folderId = null
        )

        assertTrue(note.matchesQuery("important"))
        assertTrue(note.matchesQuery("IMPORTANT")) // Case insensitive
    }

    @Test
    fun `matchesQuery finds match in content`() {
        val note = Note(
            title = "Test",
            content = "This is special content",
            folderId = null
        )

        assertTrue(note.matchesQuery("special"))
    }

    @Test
    fun `matchesQuery finds match in tags`() {
        val note = Note(
            title = "Test",
            content = "Content",
            folderId = null,
            tags = listOf("kotlin", "android")
        )

        assertTrue(note.matchesQuery("kotlin"))
        assertTrue(note.matchesQuery("android"))
    }

    @Test
    fun `matchesQuery returns false for no match`() {
        val note = Note(
            title = "Test",
            content = "Content",
            folderId = null,
            tags = listOf("tag1")
        )

        assertFalse(note.matchesQuery("nonexistent"))
    }

    @Test
    fun `NoteStatus values are correct`() {
        assertEquals(5, NoteStatus.entries.size)
        assertTrue(NoteStatus.entries.contains(NoteStatus.NONE))
        assertTrue(NoteStatus.entries.contains(NoteStatus.IN_PROGRESS))
        assertTrue(NoteStatus.entries.contains(NoteStatus.COMPLETED))
        assertTrue(NoteStatus.entries.contains(NoteStatus.ON_HOLD))
        assertTrue(NoteStatus.entries.contains(NoteStatus.ARCHIVED))
    }

    @Test
    fun `Note default values are correct`() {
        val note = Note(
            title = "Test",
            content = "",
            folderId = null
        )

        assertFalse(note.isPinned)
        assertFalse(note.isFavorite)
        assertEquals(NoteStatus.NONE, note.status)
        assertTrue(note.tags.isEmpty())
    }
}
